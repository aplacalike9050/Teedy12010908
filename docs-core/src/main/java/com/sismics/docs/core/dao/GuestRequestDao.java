package com.sismics.docs.core.dao;


import com.sismics.docs.core.model.jpa.GuestRequest;
import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.model.jpa.Loggable;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.util.context.ThreadLocalContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public class GuestRequestDao {

    /** 创建新请求，不手动设置主键，ID 由数据库生成 */
    public Long create(GuestRequest request, String userId) {
        request.setRequestedAt(LocalDateTime.now());
        request.setStatus("PENDING");
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(request);
        AuditLogUtil.create((Loggable) request, AuditLogType.CREATE, userId);
        return request.getId();
    }

    /** 根据 ID 查找 */
    public Optional<GuestRequest> findById(Long id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        TypedQuery<GuestRequest> q = em.createQuery(
                "SELECT g FROM GuestRequest g WHERE g.id = :id",
                GuestRequest.class
        );
        q.setParameter("id", id);
        try {
            return Optional.of(q.getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    /** 列出所有请求 */
    public List<GuestRequest> listAll() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        TypedQuery<GuestRequest> q = em.createQuery(
                "SELECT g FROM GuestRequest g ORDER BY g.requestedAt DESC",
                GuestRequest.class
        );
        return q.getResultList();
    }

    /**
     * 审批：accept=true 标记 ACCEPTED，否则 REJECTED
     */
    public Optional<GuestRequest> decide(Long id, boolean accept, String userId) {
        Optional<GuestRequest> opt = findById(id);
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        GuestRequest g = opt.get();
        g.setStatus(accept ? "ACCEPTED" : "REJECTED");
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        GuestRequest merged = em.merge(g);
        AuditLogUtil.create((Loggable) merged, AuditLogType.UPDATE, userId);
        return Optional.of(merged);
    }
}