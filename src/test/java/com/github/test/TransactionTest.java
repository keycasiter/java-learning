package com.github.test;

import com.github.java.learning.spring.transaction.DiyTransactionSynchronization;
import org.junit.Test;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;

/**
 * created by guanjian on 2020/12/4 8:58
 */
public class TransactionTest extends BaseTest {

    @Resource(name = "transactionTemplate")
    private TransactionTemplate transactionTemplate;

    @Test
    public void test_tx_propagation_required() {
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        boolean result = transactionTemplate.execute(transactionStatus -> {
            try {
                 boolean nested = test_nested_tx();
                System.out.println(" inner result = " + nested);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                transactionStatus.setRollbackOnly();
                return false;
            }
        });

        System.out.println("outter result = " + result);
    }

    private boolean test_nested_tx(){
        return transactionTemplate.execute(transactionStatus -> {
            try {
                throw new RuntimeException();
                //return true;
            } catch (Exception e) {
                e.printStackTrace();
                transactionStatus.setRollbackOnly();
                return false;
            }
        });
    }

    @Test
    public void test_registerSynchronization_succ() {
        transactionTemplate.execute(transactionStatus -> {
            try {
                TransactionSynchronizationManager.registerSynchronization(new DiyTransactionSynchronization());
                System.out.println("执行业务方法A");
                System.out.println("执行业务方法B");

                return true;
            } catch (Exception e) {
                System.out.println("事务回滚");
                e.printStackTrace();
                transactionStatus.setRollbackOnly();
                return false;
            }
        });
    }

    @Test
    public void test_registerSynchronization_error() {
        transactionTemplate.execute(transactionStatus -> {
            TransactionSynchronizationManager.registerSynchronization(new DiyTransactionSynchronization());

            try {
                System.out.println("执行业务方法A");
                if (true) {
                    System.out.println("执行业务方法A异常");
                    throw new RuntimeException("执行业务方法A异常");
                }
                System.out.println("执行业务方法B");

                return true;
            } catch (Exception e) {
                System.out.println("事务回滚");
                e.printStackTrace();
                transactionStatus.setRollbackOnly();
                return false;
            }
        });
    }
}
