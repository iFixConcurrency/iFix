package fix.iDebugger.nodes;

import java.util.Objects;

/**
 * @author ann
 * @param <K>
 * @param <V>
 */
public class Pair<K, V> {
    private K v1;

    private V v2;

    public static <K, V> Pair<K, V> make(K v1, V v2){
        Pair pair = new Pair();
        pair.v1 = v1;
        pair.v2 = v2;
        return pair;
    }

    public K getV1() {
        return v1;
    }

    public void setV1(K v1) {
        this.v1 = v1;
    }

    public V getV2() {
        return v2;
    }

    public void setV2(V v2) {
        this.v2 = v2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(v1, pair.v1) &&
                Objects.equals(v2, pair.v2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(v1, v2);
    }
}
