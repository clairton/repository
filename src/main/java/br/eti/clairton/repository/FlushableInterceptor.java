package br.eti.clairton.repository;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.TransactionRequiredException;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

import org.apache.logging.log4j.Logger;

/**
 * {@link Interceptor} para transacao.
 * 
 * @author Clairton Rodrigo Heinzen<clairton.rodrigo@gmail.com>
 */
@Interceptor
@Flushable
@Priority(Interceptor.Priority.LIBRARY_AFTER - 1)
public class FlushableInterceptor {
	private final EntityManager entityManager;
	private final Logger logger;

	@Inject
	public FlushableInterceptor(@NotNull final EntityManager entityManager,
			@NotNull final Logger logger) {
		super();
		this.entityManager = entityManager;
		this.logger = logger;
	}

	/**
	 * Intercepta os metodos anotados com {@link Transactional}
	 * 
	 * @param invocationContext
	 *            contexto
	 * @return retorno do metodo interceptado
	 * @throws Exception
	 *             caso ocorra algum problema
	 */
	@AroundInvoke
	public Object runFlush(final InvocationContext invocationContext) throws Exception {
		final Object object = invocationContext.proceed();
		logger.info("Executando Flush no Banco de dados");
		try {
			entityManager.joinTransaction();
		} catch (final TransactionRequiredException e) {
		}
		try {
			entityManager.flush();
		} catch (final TransactionRequiredException e) {
			logger.warn("Não há transação em andamento para rodar o EntityManager#flush");
			throw e;
		}
		return object;
	}
}
