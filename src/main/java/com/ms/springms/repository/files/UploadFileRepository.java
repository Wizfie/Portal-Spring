package com.ms.springms.repository.files;

import com.ms.springms.entity.Steps;
import com.ms.springms.entity.Registration;
import com.ms.springms.entity.UploadFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UploadFileRepository  extends JpaRepository<UploadFiles , Long> {


    List<UploadFiles> findByRegistration(Registration registration);



    List<UploadFiles> findByStepsAndRegistration(Steps steps, Registration registration);


    // Metode untuk mencari semua file berdasarkan registrationId dan isRisalah true


    boolean existsByRegistration_RegistrationIdAndApprovalStatusAndIsRisalahTrue(Long registrationId, String approve);


    List<UploadFiles> findAllByRegistration_RegistrationIdAndIsRisalahTrue(Long registrationId);

    List<UploadFiles> findAllByRegistration_RegistrationIdAndApprovalStatusAndIsRisalah(Long registrationId, String approve, boolean b);
}
