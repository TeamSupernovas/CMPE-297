package edu.sjsu.stealdeal.ups.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.sjsu.stealdeal.ups.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
