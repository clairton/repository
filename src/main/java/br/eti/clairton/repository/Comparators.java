package br.eti.clairton.repository;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

/**
 * Enum of {@link Comparator}.
 *
 * @author Clairton Rodrigo Heinzen clairton.rodrigo@gmail.com
 */
public enum Comparators implements Comparator {
    GREATER_THAN_OR_EQUAL(new GreaterThanOrEqual()),
    LESS_THAN_OR_EQUAL(new LessThanOrEqual()),
    EQUAL(new Equal()),
    EQUAL_IGNORE_CASE(new EqualIgnoreCase()),
    NOT_NULL(new NotNull()),
    NULL(new Null()),
    EXIST(new Exist()),
    NOT_EQUAL(new NotEqual()),
    LIKE(new Like()),
    STARTS_WITH(new StartsWith()),
    NOT_STARTS_WITH(new NotStartsWith()),
    ENDS_WITH(new EndsWith()),
    NOT_ENDS_WITH(new NotEndsWith()),
    NOT_LIKE(new NotLike()),
    LESS_THAN(new LessThan()),
    GREATER_THAN(new GreaterThan());
    private final Comparator comparator;

    private static Map<String, Comparator> repository;

    private Comparators(final Comparator comparator) {
        this.comparator = comparator;
        put();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return comparator.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate build(final CriteriaBuilder cb, final Expression<?> x, final Object y) {
        return comparator.build(cb, x, y);
    }

    public static Comparator bySymbol(final String symbol) {
        if (repository.containsKey(symbol)) {
            return repository.get(symbol);
        } else {
            throw new RuntimeException("Operator " + symbol + " não encontrado");
        }
    }

    private void put() {
        if (repository == null) {
            repository = new HashMap<String, Comparator>();
        }
        repository.put(comparator.toString(), comparator);
    }
}
