package edu.unlu.sdypp.ej6;

import java.rmi.RemoteException;

public class Implementation implements Services {
    @Override
    public int[] sumar(int[] v1, int[] v2) throws RemoteException {
        int[] v3 = new int[Math.max(v1.length, v2.length)];
        for (int i = 0; i < v3.length; i++) {
            if (v1.length > i) {
                v3[i] += v1[i];
            }
            if (v2.length > i) {
                v3[i] += v2[i];
            }
        }

        // Modifico arbitrariamente los vectores suministrados
        v1 = new int[]{9,9,9};
        v2 = new int[]{9,9,9};

        return v3;
    }

    @Override
    public int[] restar(int[] v1, int[] v2) throws RemoteException {
        int[] v3 = new int[Math.max(v1.length, v2.length)];
        for (int i = 0; i < v3.length; i++) {
            if (v1.length > i) {
                v3[i] = v1[i];
            }
            if (v2.length > i) {
                v3[i] -= v2[i];
            }
        }

        // Modifico arbitrariamente los vectores suministrados
        v1 = new int[]{9,9,9};
        v2 = new int[]{9,9,9};

        return v3;
    }
}
