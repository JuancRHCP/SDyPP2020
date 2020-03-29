package edu.unlu.sdypp.ej6;

import java.rmi.RemoteException;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class Implementation implements Services {
	
	private static final Logger logger = (Logger) LoggerFactory.getLogger(Implementation.class);
	
    @Override
    public int[] sumar(int[] v1, int[] v2) throws RemoteException {
    	if (v1 == null && v2 == null) {
    		logger.error("v1 && v2 are null");
    		throw new RuntimeException("v1 && v2 are null");
    	}
    	if (v1 == null)
    		return v2;
    	if (v2 == null)
    		return v1;
    		
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
    	if (v1 == null && v2 == null) {
    		logger.error("v1 && v2 are null");
    		throw new RuntimeException("v1 && v2 are null");
    	}
    	if (v1 == null)
    		return v2;
    	if (v2 == null)
    		return v1;
    	
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
