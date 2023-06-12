package com.bonitasoft.technicalchallenge.repository;

import com.bonitasoft.technicalchallenge.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
