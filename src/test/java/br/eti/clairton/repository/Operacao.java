package br.eti.clairton.repository;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Representa uma ação.
 * 
 * @author Clairton Rodrigo Heinzen<clairton.rodrigo@gmail.com>
 */
@Entity
@Table(name = "operacoes")
public class Operacao extends br.eti.clairton.repository.Model {
    private static final long serialVersionUID = 1L;
    
    @NotNull
    @Size(min = 1, max = 50)
    @Identificador
    private final String nome;
    
    @ManyToOne
    @Identificador
    @NotNull
    private final Recurso recurso;
    
    /**
     * Construtor padrão.
     */
    @Deprecated
    public Operacao() {
        this(null, null);
    }
    
    /**
     * Construtor com parametros.
     * 
     * @param recurso
     *            recurso ao qual a operacao pertence
     * @param nome
     *            nome da ação
     */
    public Operacao(final Recurso recurso, final String nome) {
        super();
        this.nome = nome;
        this.recurso = recurso;
        if (recurso != null) {
            recurso.adicionar(this);
        }
    }
    
    public String getNome() {
        return nome;
    }
    
    public Recurso getRecurso() {
        return recurso;
    }
}
