package br.eti.clairton.repository;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

/**
 * Enum of {@link Operator}.
 * 
 * @author Clairton Rodrigo Heinzen<clairton.rodrigo@gmail.com>
 */
public enum Operators implements Operator {
    GREATER_THAN_OR_EQUAL(new GreaterThanOrEqual()),
    LESS_THAN_OR_EQUAL(new LessThanOrEqual()),
    EQUAL(new Equal()),
    EQUAL_IGNORE_CASE(new EqualIgnoreCase()),
    NOT_NULL(new NotNull()),
    IN(new In()),
    NOT_IN(new NotIn()),
    EXIST(new Exist()),
    NOT_EQUAL(new NotEqual()),
    LIKE(new Like()),
    NOT_LIKE(new NotLike()),
    LESS_THAN(new LessThan()),
    GREATER_THAN(new GreaterThan());
    private final Operator operator;
    
    private static Map<String, Operator> repository;
    
    private Operators(final Operator operator) {
        this.operator = operator;
        put();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate build(final CriteriaBuilder cb, final Expression<?> x, final Object y) {
        return operator.build(cb, x, y);
    }
    
    public static Operator bySymbol(final String symbol) {
        if (repository.containsKey(symbol)) {
            return repository.get(symbol);
        } else {
            throw new RuntimeException("Operator " + symbol + " não encontrado");
        }
    }
    
    private void put() {
        if (repository == null) {
            repository = new HashMap<>();
        }
        repository.put(operator.toString(), operator);
    }
}
