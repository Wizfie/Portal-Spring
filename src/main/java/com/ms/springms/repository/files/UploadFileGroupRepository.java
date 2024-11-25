package com.ms.springms.repository.files;

import com.ms.springms.entity.UploadFileGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface  UploadFileGroupRepository  extends JpaRepository < UploadFileGroup , Long> {
}
