package br.eti.clairton.repository;

import java.util.ConcurrentModificationException;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

import org.apache.logging.log4j.Logger;

/**
 * {@link Interceptor} para transacao.
 * 
 * @author Clairton Rodrigo Heinzen<clairton.rodrigo@gmail.com>
 */
@Interceptor
@Transactional
public class TransactionalInterceptor {
    private final EntityManager entityManager;
    private final Logger logger;
    
    @Inject
    public TransactionalInterceptor(@NotNull final EntityManager entityManager,@NotNull final Logger logger) {
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
    public Object addTransaction(final InvocationContext invocationContext) throws Exception {
        if (!entityManager.getTransaction().isActive()) {
        	logger.debug("Iniciando uma nova transação no banco de dados");
            entityManager.getTransaction().begin();
        }else{
        	logger.debug("Transação já está ativa");
        }
        try {
            final Object object = invocationContext.proceed();
            if (entityManager.getTransaction().getRollbackOnly()) {
            	logger.debug("Cancelando transação no banco de dados");
                entityManager.getTransaction().rollback();
            } else {
            	try{
            		entityManager.flush();
            		logger.debug("Confirmando transação no banco de dados");
            		//não precisa commitar pois não haverá transação ativa
            		entityManager.getTransaction().commit();
            	}catch (final ConcurrentModificationException e) {
                	logger.error("Houve um erro de concorrência ao sincronizar os dados com o banco de dados, detalhes: {}", e.getMessage());
                	logger.debug("Detalhes: ", e);
				}
            }
            return object;
        } catch (final Exception e) {
        	logger.warn("Houve um erro ao controlar a transação no banco de dados, detalhes: {}", e.getMessage());
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
		}
	}
}
