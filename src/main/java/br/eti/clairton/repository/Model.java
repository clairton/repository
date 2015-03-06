package br.eti.clairton.repository;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import br.eti.clairton.identificator.Identificable;

/**
 * Contrato abstrato para as entidades.
 * 
 * @author Clairton Rodrigo Heinzen<clairton.rodrigo@gmail.com>
 */
@MappedSuperclass
@Cacheable
public abstract class Model extends Identificable implements Serializable,
		Cloneable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	public Long getId() {
		return id;
	}

	/**
	 * Clona o objeto, setando como null o id. {@inheritDoc}.
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		final Model entidade = (Model) super.clone();
		entidade.id = null;
		return entidade;
	}
}
