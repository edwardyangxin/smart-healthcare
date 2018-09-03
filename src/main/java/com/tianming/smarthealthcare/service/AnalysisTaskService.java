package com.tianming.smarthealthcare.service;

import com.tianming.smarthealthcare.domain.*;
import com.tianming.smarthealthcare.repository.*;
import com.tianming.smarthealthcare.web.rest.vm.AnalysisTaskVM;
import com.tianming.smarthealthcare.web.rest.vm.DiagnoseTaskVM;
import com.tianming.smarthealthcare.web.rest.vm.ExamResultVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.NoSuchFileException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AnalysisTaskService {
    private final Logger log = LoggerFactory.getLogger(AnalysisTaskService.class);

    private AnalysisTaskRepository analysisTaskRepository;
    private PatientRepository patientRepository;
    private StorageRepository storageRepository;
    private DemoRepository demoRepository;
    private AbnormalAnalysisRepository abnormalAnalysisRepository;

    public AnalysisTaskService(AnalysisTaskRepository analysisTaskRepository,
                               PatientRepository patientRepository,
                               StorageRepository storageRepository,
                               DemoRepository demoRepository, AbnormalAnalysisRepository abnormalAnalysisRepository) {
        this.analysisTaskRepository = analysisTaskRepository;
        this.patientRepository = patientRepository;
        this.storageRepository = storageRepository;
        this.demoRepository = demoRepository;
        this.abnormalAnalysisRepository = abnormalAnalysisRepository;
    }

    public AnalysisTask create(AnalysisTaskVM analysisTaskVM) throws NoSuchPatientException, NoSuchFileException {
        Optional<Patient> patient = patientRepository.findById(analysisTaskVM.getPatientId());
        if (!patient.isPresent()) {throw new NoSuchPatientException();}
        Optional<Storage> file = storageRepository.findById(analysisTaskVM.getXrayId());
        if (!file.isPresent()) {throw new NoSuchFileException("no such xray file");}

        AnalysisTask analysisTask = new AnalysisTask();
        BeanUtils.copyProperties(analysisTaskVM, analysisTask);
        analysisTask.setPatient(patient.get());

        //get demo result
        Storage f = file.get();
        Optional<Demo> demoResult = demoRepository.findByDemoName(f.getOriginalName());
        if (demoResult.isPresent()) {
            //demo data
            analysisTask.setAnalysisStatus(1);
            analysisTask.setAnalysisResult(demoResult.get().getDemoResult());
        }

        return analysisTaskRepository.save(analysisTask);
    }

    public AnalysisTask modify(DiagnoseTaskVM diagnoseTaskVM) {
        AnalysisTask analysisTask = analysisTaskRepository.findOne(diagnoseTaskVM.getId());
        analysisTask.setDiagnosisResult(diagnoseTaskVM.getDiagnosisResult());
        analysisTask.setDiagnosisComment(diagnoseTaskVM.getDiagnosisComment());
        return analysisTaskRepository.save(analysisTask);
    }

    public Page<AnalysisTask> getTasks(String username, Pageable pageable) {
        return analysisTaskRepository.findByCreatedBy(username, pageable);
    }

    public AnalysisTask getTask(Long taskId) {
        return analysisTaskRepository.findOne(taskId);
    }

    public AnalysisTask createTask(Storage storage, Patient patient) {
        AbnormalAnalysis abnormalAnalysis = new AbnormalAnalysis();
        abnormalAnalysisRepository.save(abnormalAnalysis);

        AnalysisTask analysisTask = new AnalysisTask();
        analysisTask.setPatient(patient);
        analysisTask.setXrayId(storage.getId());
        analysisTask.setAbnormalAnalysis(abnormalAnalysis);
        return analysisTaskRepository.save(analysisTask);
    }

    public List<AnalysisTask> getAllTasks(String username) {
        return analysisTaskRepository.findByCreatedByOrderByCreatedDateDesc(username);
    }

    public List<AnalysisTask> getAllTasks() {
        return analysisTaskRepository.findAllByOrderByCreatedDateDesc();
    }

    public ExamResultVM countExamResult() {
        Long totalTask = analysisTaskRepository.count();

        Long totalSuspectedCases = analysisTaskRepository.countTotalSuspectedCases();
        Long totalConfirmedCases = analysisTaskRepository.countTotalConfirmedCases();
        LocalDateTime startOfToday = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

        Long todayTask = analysisTaskRepository.countTaskCreatedDateAfter(startOfToday);
        Long todaySuspectedCases = analysisTaskRepository.countSuspectedCasesByLastModifiedDateAfter(startOfToday);
        Long todayConfirmedCases = analysisTaskRepository.countConfirmedCasesByLastModifiedDateAfter(startOfToday);

        ExamResultVM examResultVM = new ExamResultVM();
        examResultVM.setTotalTask(totalTask);
        examResultVM.setSuspected(totalSuspectedCases);
        examResultVM.setConfirmed(totalConfirmedCases);
        examResultVM.setTodayTask(todayTask);
        examResultVM.setTodaySuspected(todaySuspectedCases);
        examResultVM.setTodayConfirmed(todayConfirmedCases);

        return examResultVM;
    }
}
