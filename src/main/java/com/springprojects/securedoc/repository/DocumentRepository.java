package com.springprojects.securedoc.repository;

import static com.springprojects.securedoc.constant.Constants.SELECT_COUNT_DOCUMENTS_QUERY;
import static com.springprojects.securedoc.constant.Constants.SELECT_COUNT_DOCUMENTS_BY_NAME_QUERY;
import static com.springprojects.securedoc.constant.Constants.SELECT_DOCUMENTS_QUERY;
import static com.springprojects.securedoc.constant.Constants.SELECT_DOCUMENT_QUERY;
import static com.springprojects.securedoc.constant.Constants.SELECT_DOCUMENTS_BY_NAME_QUERY;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.springprojects.securedoc.dto.api.IDocument;
import com.springprojects.securedoc.entity.DocumentEntity;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
	 @Query(countQuery = SELECT_COUNT_DOCUMENTS_QUERY, value = SELECT_DOCUMENTS_QUERY, nativeQuery = true)
	 Page<IDocument> findDocuments(Pageable pageable);
	 
	 @Query(countQuery = SELECT_COUNT_DOCUMENTS_BY_NAME_QUERY, value = SELECT_DOCUMENTS_BY_NAME_QUERY, nativeQuery = true)
	 Page<IDocument> findDocumentsByName(@Param("documentName") String documentName, Pageable pageable);
	 
	 @Query(value = SELECT_DOCUMENT_QUERY, nativeQuery = true)
	 Optional <IDocument> findDocumentByDocumentId(String documentId);

	 Optional<DocumentEntity> findByDocumentId(String documentId);
}
