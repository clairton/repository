package br.eti.clairton.repository;

import java.io.Serializable;

import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.Attribute;
import javax.validation.constraints.NotNull;

/**
 * Predicado para consulta.
 * 
 * @author Clairton Rodrigo Heinzen<clairton.rodrigo@gmail.com>
 */
public class Predicate implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Object value;
    
    private Attribute<?, ?>[] attributes;
    
    private JoinType joinType = JoinType.INNER;
    
    private Operator operator = new Equal();
    
    public <T> Predicate(final @NotNull T value, final @NotNull Attribute<?, ?>... attributes) {
        super();
        this.value = value;
        this.attributes = attributes;
    }
    
    public <T> Predicate(final @NotNull T value, final @NotNull Operator operator,
            final @NotNull Attribute<?, ?>... attribute) {
        super();
        this.value = value;
        this.attributes = attribute;
        this.operator = operator;
    }
    
    public Predicate(final @NotNull Attribute<?, ?>... attribute) {
        super();
        this.attributes = attribute;
    }
    
    public Predicate(final @NotNull Operator operator, final @NotNull Attribute<?, ?>... attribute) {
        super();
        this.operator = operator;
        this.attributes = attribute;
    }
    
    public Predicate(final @NotNull JoinType joinType, final @NotNull Operator operator,
            final @NotNull Attribute<?, ?>... attribute) {
        super();
        this.attributes = attribute;
        this.joinType = joinType;
        this.operator = operator;
    }
    
    public <T> Predicate(final @NotNull T value, JoinType joinType, final @NotNull Operator operator,
            final @NotNull Attribute<?, ?>... attribute) {
        super();
        this.value = value;
        this.attributes = attribute;
        this.joinType = joinType;
        this.operator = operator;
    }
    
    public Attribute<?, ?>[] getAttributes() {
        return attributes;
    }
    
    public Attribute<?, ?> getAttribute() {
        return attributes[0];
    }
    
    public <T> void setValue(final @NotNull T value) {
        this.value = value;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getValue() {
        return ( T ) value;
    }
    
    public Operator getOperator() {
        return operator;
    }
    
    public JoinType getJoinType() {
        return joinType;
    }
    
    public void setJoinType(final @NotNull JoinType joinType) {
        this.joinType = joinType;
    }
    
    public void setValueObject(final @NotNull Object value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return joinType.toString() + " " + path() + " " + operator.toString() + " " + value.toString();
    }
    
    private String path() {
        final StringBuilder path = new StringBuilder();
        String separator = "";
        for (final Attribute<?, ?> a : attributes) {
            path.append(separator + a.getName());
            separator = ".";
        }
        return path.toString();
    }
}
