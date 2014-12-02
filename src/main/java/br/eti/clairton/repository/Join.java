package br.eti.clairton.repository;

import java.io.Serializable;

import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;

/**
 * Trata um relacionamento no Plural.
 * 
 * @author Clairton Rodrigo Heinzen<clairton.rodrigo@gmail.com>
 */
interface Join extends Serializable {
    public static final Join SET = new Join() {
        private static final long serialVersionUID = 1L;
        
        /**
         * {@inheritDoc}
         */
        @Override
        public <T, Y> javax.persistence.criteria.Join<T, Y> join(final From<T, Y> from, final JoinType joinType,
                final PluralAttribute<?, ?, ?> attribute) {
            @SuppressWarnings("unchecked")
            final SetAttribute<? super Y, Y> attributes = ( SetAttribute<? super Y, Y> ) attribute;
            @SuppressWarnings("unchecked")
            final javax.persistence.criteria.Join<T, Y> join = ( javax.persistence.criteria.Join<T, Y> ) from.join(
                    attributes, joinType);
            return join;
        }
    };
    
    public static final Join LIST = new Join() {
        private static final long serialVersionUID = 1L;
        
        /**
         * {@inheritDoc}
         */
        @Override
        public <T, Y> javax.persistence.criteria.Join<T, Y> join(final From<T, Y> from, final JoinType joinType,
                final PluralAttribute<?, ?, ?> attribute) {
            @SuppressWarnings("unchecked")
            final ListAttribute<? super Y, Y> attributes = ( ListAttribute<? super Y, Y> ) attribute;
            @SuppressWarnings("unchecked")
            final javax.persistence.criteria.Join<T, Y> join = ( javax.persistence.criteria.Join<T, Y> ) from.join(
                    attributes, joinType);
            return join;
        }
    };
    
    public static final Join COLLECTION = new Join() {
        private static final long serialVersionUID = 1L;
        
        /**
         * {@inheritDoc}
         */
        @Override
        public <T, Y> javax.persistence.criteria.Join<T, Y> join(final From<T, Y> from, final JoinType joinType,
                final PluralAttribute<?, ?, ?> attribute) {
            @SuppressWarnings("unchecked")
            final CollectionAttribute<? super Y, Y> attributes = ( CollectionAttribute<? super Y, Y> ) attribute;
            @SuppressWarnings("unchecked")
            final javax.persistence.criteria.Join<T, Y> join = ( javax.persistence.criteria.Join<T, Y> ) from.join(
                    attributes, joinType);
            return join;
        }
    };
    
    public static final Join MAP = new Join() {
        private static final long serialVersionUID = 1L;
        
        /**
         * {@inheritDoc}
         */
        @Override
        public <T, Y> javax.persistence.criteria.Join<T, Y> join(final From<T, Y> from, final JoinType joinType,
                final PluralAttribute<?, ?, ?> attribute) {
            @SuppressWarnings("rawtypes")
            final MapAttribute attributes = ( MapAttribute ) attribute;
            @SuppressWarnings("unchecked")
            final javax.persistence.criteria.Join<T, Y> join = ( javax.persistence.criteria.Join<T, Y> ) from.join(
                    attributes, joinType);
            return join;
        }
    };
    
    /**
     * Cria o join.
     * 
     * @param from
     *            {@link From}
     * @param joinType
     *            {@link JoinType}
     * @param attribute
     *            {@link PluralAttribute}
     * @return {@link Join}
     */
    <T, Y> javax.persistence.criteria.Join<T, Y> join(From<T, Y> from, JoinType joinType,
            PluralAttribute<?, ?, ?> attribute);
}