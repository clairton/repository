package br.eti.clairton.repository;

import static br.eti.clairton.identificator.Identificator.Type.TO_STRING;
import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import br.eti.clairton.identificator.Identificable;
import br.eti.clairton.identificator.Identificator;

/**
 * Contrato abstrato para as entidades.
 * 
 * @author Clairton Rodrigo Heinzen<clairton.rodrigo@gmail.com>
 */
@Cacheable
@MappedSuperclass
public abstract class Model extends Identificable implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;

	@Id
	@Identificator(TO_STRING)
	@GeneratedValue(strategy = IDENTITY)
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
