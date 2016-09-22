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
 * Abstract contract fir entities.
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
	 * {@inheritDoc}.</br>
	 * Clone the object with null in id.
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		final Model entidade = (Model) super.clone();
		entidade.id = null;
		return entidade;
	}

	/**
	 * Return id the instance is New.
	 * 
	 * The default test, if not have id, is new
	 * 
	 * @return True/False
	 */
	public Boolean isNew() {
		return id == null;
	}

	/**
	 * Return id the instance is Managed.
	 * 
	 * The default test, if have id, is managed
	 * 
	 * @return True/False
	 */
	public Boolean isManaged() {
		return id != null;
	}

	/**
	 * Copy the instance usando {@link Model#clone()}
	 * 
	 * @see Model#clone()
	 * 
	 * @return T instance with values
	 */
	public <T extends Model> T copy() {
		try {
			@SuppressWarnings("unchecked")
			final T copy = (T) clone();
			return copy;
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
