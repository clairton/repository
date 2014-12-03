package br.eti.clairton.repository;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

/**
 * {@link Interceptor} para transacao.
 * 
 * @author Clairton Rodrigo Heinzen<clairton.rodrigo@gmail.com>
 */
@Interceptor
@Transactional
public class TransactionalInterceptor {
    private final EntityManager entityManager;
    
    @Inject
    public TransactionalInterceptor(final EntityManager entityManager) {
        super();
        this.entityManager = entityManager;
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
    public Object addTransaction(final InvocationContext invocationContext) throws Exception {
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        try {
            final Object object = invocationContext.proceed();
            if (entityManager.getTransaction().getRollbackOnly()) {
                entityManager.getTransaction().rollback();
            } else {
                entityManager.getTransaction().commit();
            }
            return object;
        } catch (final Throwable e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
		}
	}
}
