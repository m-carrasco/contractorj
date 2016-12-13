package contractorj.construction;

import contractorj.model.Action;
import j2bpl.Class;
import j2bpl.Method;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ActionsExtractor {

    private final static String INVARIANT_METHOD_NAME = "inv()";

    private final Class theClass;

    private final Map<String, Method> methodsMap = new HashMap<>();

    private final Set<Action> instanceActions = new HashSet<>();

    private final Set<Action> constructorActions = new HashSet<>();

    private Method invariant;

    public Set<Action> getInstanceActions() {

        return instanceActions;
    }

    public Set<Action> getConstructorActions() {

        return constructorActions;
    }

    public Method getInvariant() {

        return invariant;
    }

    public ActionsExtractor(Class theClass) {

        this.theClass = theClass;

        computeMethodsMap();
        searchInvariant();
        generateActions();
    }

    private void generateActions() {

        for (String methodName : methodsMap.keySet()) {

            if (methodName.equals(INVARIANT_METHOD_NAME)) {
                continue;
            }

            if (isPreconditionName(methodName)) {
                continue;
            }

            final Method method = methodsMap.get(methodName);

            if (method.isStatic()) {
                continue;
            }

            final String statePreconditionMethodName = getStatePreconditionMethodName(method);
            final Method statePrecondition = methodsMap.getOrDefault(statePreconditionMethodName, null);

            final Method paramsPrecondition;

            if (hasNonThisParameters(method)) {

                final String paramsPreconditionMethodName = getParamsPreconditionMethodName(method);
                paramsPrecondition = methodsMap.getOrDefault(paramsPreconditionMethodName, null);
            } else {
                paramsPrecondition = null;
            }

            validatePreconditions(method, statePrecondition, paramsPrecondition);

            final Action action = new Action(method, statePrecondition, paramsPrecondition);

            if (method.isConstructor()) {
                constructorActions.add(action);
            } else {
                instanceActions.add(action);
            }
        }
    }

    private boolean isPreconditionName(final String methodName) {

        return methodName.contains("_pre(");
    }

    private void validatePreconditions(final Method method,
                                       final Method statePrecondition,
                                       final Method paramsPrecondition) {

        if (statePrecondition != null) {

            final String statePreconditionName = statePrecondition.getJavaNameWithArgumentTypes();

            if (statePrecondition.getParameterTypes().size() > 0) {
                throw new IllegalArgumentException("State precondition " + statePreconditionName
                        + " must have no argument.");
            }

            if (!statePrecondition.hasReturnType() || !statePrecondition.getTranslatedReturnType().equals("bool")) {
                throw new IllegalArgumentException("Precondition " + statePreconditionName
                        + " must return a boolean");
            }

        }

        if (paramsPrecondition != null) {

            final String paramsPreconditionName = paramsPrecondition.getJavaNameWithArgumentTypes();

            if (!paramsPrecondition.hasReturnType() || !paramsPrecondition.getTranslatedReturnType().equals("bool")) {
                throw new IllegalArgumentException("Precondition " + paramsPreconditionName
                        + " must return a boolean");
            }

            if (!method.getParameterTypes().equals(paramsPrecondition.getParameterTypes())) {
                throw new IllegalArgumentException("Parameters precondition " + paramsPreconditionName
                        + " must have the same arguments as its method.");
            }
        }
    }

    private boolean hasNonThisParameters(final Method method) {

        return method.getParameterTypes().size() > 0;
    }

    private String getStatePreconditionMethodName(final Method method) {

        return method.getJavaNameWithArgumentTypes().replaceAll("\\(.*\\)", "_pre()");
    }

    private String getParamsPreconditionMethodName(final Method method) {

        return method.getJavaNameWithArgumentTypes().replace("(", "_pre(");
    }

    private void searchInvariant() {

        if (!methodsMap.containsKey(INVARIANT_METHOD_NAME)) {
            throw new UnsupportedOperationException("Invariant method name missing. It must be named: "
                    + INVARIANT_METHOD_NAME);
        }

        invariant = methodsMap.get(INVARIANT_METHOD_NAME);

        if (!invariant.getTranslatedReturnType().equals("bool")) {
            throw new IllegalArgumentException("Invariant method must return a boolean");
        }

        if (invariant.getTranslatedArgumentTypes().size() != 1) {
            throw new IllegalArgumentException("Invariant method must have 0 arity");
        }
    }

    private void computeMethodsMap() {

        for (Method method : theClass.getMethods()) {

            final String methodName = method.getJavaNameWithArgumentTypes();

            methodsMap.put(methodName, method);
        }
    }

}
