package edu.unlu.sdypp.ej4.compute;

/**
 * Guia: https://docs.oracle.com/javase/tutorial/rmi/implementing.html
 */
public class ImplementacionComputable implements Computable {

    public ImplementacionComputable() {
        super();
    }

    public <T> T ejecutarTarea(Tarea<T> t) {
        return t.ejecutar();
    }
}
