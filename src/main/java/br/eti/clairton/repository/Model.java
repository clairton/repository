package br.eti.clairton.repository;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;

/**
 * Contrato abstrato para as entidades.
 * 
 * @author Clairton Rodrigo Heinzen<clairton.rodrigo@gmail.com>
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Model implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue
    private Long id;
    
    public Long getId() {
        return id;
    }
    
    /**
     * Clona o objeto, setando como null o id. {@inheritDoc}.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        final Model entidade = ( Model ) super.clone();
        entidade.id = null;
        return entidade;
    }
    
    @Override
    public int hashCode() {
        // TODO criar baseado na anotação Identificador
        return super.hashCode();
    }
    
    @Override
    public String toString() {
        // TODO criar baseado na anotação Identificador
        return super.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        // TODO criar baseado na anotação Identificador
        return super.equals(obj);
    }
}
