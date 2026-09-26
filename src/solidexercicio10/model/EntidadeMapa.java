package solidexercicio10.model;

public abstract class EntidadeMapa implements Posicionavel {
    private int x;
    private int y;

    protected EntidadeMapa(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public final int getX() {
        return x;
    }

    @Override
    public final int getY() {
        return y;
    }

    protected final void setPosicao(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public abstract String getSimbolo();
}
