package br.eti.clairton.repository;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "recursos")
//TODO recurso aninhados, um recurso pode ter outros recursos dentro dele
public class Recurso extends br.eti.clairton.repository.Model {
    private static final long serialVersionUID = 1L;
    
    @NotNull
    @Identificador
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "recurso")
    private final Collection<Operacao> operacoes = new HashSet<>();
    
    @ManyToOne
    @NotNull
    @Identificador
    private final Aplicacao aplicacao;
    
    @NotNull
    @Size(min = 1, max = 50)
    @Identificador
    private final String nome;
    
    @Deprecated
    public Recurso() {
        this(null, null);
    }
    
    public Recurso(final Aplicacao aplicacao, final String nome) {
        super();
        this.nome = nome;
        this.aplicacao = aplicacao;
        if (aplicacao != null) {
            aplicacao.adicionar(this);
        }
    }
    
    public Recurso(final Aplicacao aplicacao, final String nome, final Collection<Operacao> operacoes) {
        this(aplicacao, nome);
        this.operacoes.addAll(operacoes);
    }
    
    public void adicionar(final Operacao operacao) {
        operacoes.add(operacao);
    }
    
    public void remover(final Operacao operacao) {
        operacoes.remove(operacao);
    }
    
    public Collection<Operacao> getOperacoes() {
        return Collections.unmodifiableCollection(operacoes);
    }
}
