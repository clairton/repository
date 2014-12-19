package br.eti.clairton.repository;

public class RepositoryQueryException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public RepositoryQueryException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public RepositoryQueryException(final String message) {
		super(message);
	}

}
