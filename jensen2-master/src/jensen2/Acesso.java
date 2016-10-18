package jensen2;

public enum Acesso {
    R("R"),
    W("W"),
    S("S"),
    E("E"),
    C("C"),
    A("A");
    String texto;

    private Acesso(String texto) {
        this.texto = texto;
    }

    @Override
    public String toString() {
        return texto;
    }
}
