package edu.dartmouth.cs65.artcelerationlibrary;

public class TransformTest {
    public int transformType;
    public int[] intArgs;
    public float[] floatArgs;
    TransformTest(int type, int[] args1, float[] args2){
        transformType = type;
        intArgs = args1;
        floatArgs = args2;
    }
}
