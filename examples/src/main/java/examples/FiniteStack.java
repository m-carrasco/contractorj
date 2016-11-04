package examples;

public class FiniteStack {

    public int Max;
    public int Next;

    public FiniteStack()
    {
        Max = 5;
        Next = -1;
    }

    public FiniteStack(int size)
    {
        Max = size;
        Next = -1;
    }

    public void Pop()
    {
        Next = Next - 1;
    }

    public void Push()
    {
        Next = Next + 1;
    }

    public boolean inv() {
        return Max > 2 && Next >= -1 && Max >= Next;
    }

    public boolean Pop_pre() {
        return Next > -1;
    }

    public boolean Push_pre() {
        return Next < Max;
    }
}