package com.sky.context;

/**
 * BaseContext类提供了线程安全的上下文管理，主要用于在跨不同操作或服务调用的环境中
 * 传递和存取特定于线程的数据。这对于需要在不同操作之间保持数据隔离的场合特别有用，
 * 比如在多用户环境中保持用户身份信息。
 */
public class BaseContext {

    // 使用ThreadLocal来存储每个线程独有的数据副本，避免数据共享带来的线程安全问题
    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置当前线程的ID。
     * 这个ID可以是任何需要在线程之间保持和传递的标识信息，如用户ID、事务ID等。
     *
     * @param id 需要设置的当前线程的ID。
     */
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    /**
     * 获取当前线程的ID。
     * 这个方法用于获取之前通过setCurrentId方法设置的ID，以便在后续的操作中使用。
     *
     * @return 当前线程的ID，如果之前没有设置，则返回null。
     */
    public static Long getCurrentId() {
        return threadLocal.get();
    }

    /**
     * 移除当前线程的ID。
     * 这个方法用于清理线程的ID信息，以释放资源，特别是在线程可能被重用的环境中。
     */
    public static void removeCurrentId() {
        threadLocal.remove();
    }

}
