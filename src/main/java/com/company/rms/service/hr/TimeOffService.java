package com.company.rms.service.hr;

import com.company.rms.dto.request.TimeOffCreateRequest;
import com.company.rms.dto.response.TimeOffResponse;
import com.company.rms.entity.hr.Employee;
import com.company.rms.entity.hr.TimeOffRequest;
import com.company.rms.exception.BusinessException;
import com.company.rms.repository.hr.EmployeeRepository;
import com.company.rms.repository.hr.TimeOffRequestRepository;
import com.company.rms.security.SecurityUtils;
import com.company.rms.service.general.NotificationService; // [1. Import Service Thông báo]
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeOffService {

    private final TimeOffRequestRepository timeOffRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService; // [2. Inject Service Thông báo]

    @Transactional
    public TimeOffResponse createRequest(TimeOffCreateRequest request) {
        Long currentEmpId = SecurityUtils.getCurrentEmployeeId();
        Employee employee = employeeRepository.findById(currentEmpId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy thông tin nhân viên"));
        
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        TimeOffRequest timeOff = TimeOffRequest.builder()
                .employee(employee)
                .type(TimeOffRequest.TimeOffType.valueOf(request.getType()))
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .status(TimeOffRequest.RequestStatus.PENDING)
                .build();
        
        // [Optional] Có thể thêm logic gửi thông báo cho Manager ở đây nếu muốn
        // Ví dụ: "Nhân viên A vừa gửi đơn xin nghỉ phép"
        
        return mapToResponse(timeOffRepository.save(timeOff));
    }

    @Transactional(readOnly = true)
    public List<TimeOffResponse> getMyRequests() {
        Long currentEmpId = SecurityUtils.getCurrentEmployeeId();
        // Dùng hàm có JOIN FETCH để tránh lỗi 500
        return timeOffRepository.findByEmployeeIdOrderByStartDateDesc(currentEmpId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void cancelRequest(Long id) {
        TimeOffRequest req = timeOffRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Request not found"));
        if (req.getStatus() != TimeOffRequest.RequestStatus.PENDING) {
            throw new BusinessException("Chỉ có thể hủy đơn đang chờ duyệt (Pending)");
        }
        req.setStatus(TimeOffRequest.RequestStatus.CANCELLED);
        timeOffRepository.save(req);
    }

    // --- [QUAN TRỌNG] CÁC HÀM DUYỆT ---

    @Transactional(readOnly = true)
    public List<TimeOffResponse> getPendingApprovals() {
        Long currentEmpId = SecurityUtils.getCurrentEmployeeId();
        // Dùng hàm có JOIN FETCH
        return timeOffRepository.findAllPendingExceptSelf(currentEmpId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void approveRequest(Long id) {
        processRequest(id, TimeOffRequest.RequestStatus.APPROVED);
    }

    @Transactional
    public void rejectRequest(Long id) {
        processRequest(id, TimeOffRequest.RequestStatus.REJECTED);
    }

    private void processRequest(Long id, TimeOffRequest.RequestStatus newStatus) {
        Long approverId = SecurityUtils.getCurrentEmployeeId();
        Employee approver = employeeRepository.findById(approverId)
                .orElseThrow(() -> new BusinessException("Approver not found"));

        TimeOffRequest req = timeOffRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Request not found"));

        if (req.getStatus() != TimeOffRequest.RequestStatus.PENDING) {
            throw new BusinessException("Đơn này đã được xử lý trước đó");
        }

        // Cập nhật trạng thái
        req.setStatus(newStatus);
        req.setApprover(approver);
        timeOffRepository.save(req);

        // --- [3. LOGIC GỬI THÔNG BÁO TỰ ĐỘNG] ---
        String title = "Cập nhật trạng thái nghỉ phép";
        String message;
        String type;

        if (newStatus == TimeOffRequest.RequestStatus.APPROVED) {
            message = String.format("Tin vui! Đơn nghỉ phép từ %s đến %s của bạn đã được DUYỆT.", 
                                    req.getStartDate(), req.getEndDate());
            type = "SUCCESS";
        } else {
            message = String.format("Đơn nghỉ phép từ %s đến %s của bạn đã bị TỪ CHỐI.", 
                                    req.getStartDate(), req.getEndDate());
            type = "ERROR";
        }

        // Gửi thông báo đến User ID của người làm đơn
        // Lưu ý: req.getEmployee() là Entity Employee, cần .getUser().getId() để lấy User ID
        if (req.getEmployee().getUser() != null) {
            notificationService.createNotification(
                req.getEmployee().getUser().getId(),
                title,
                message,
                type
            );
        }
    }

    private TimeOffResponse mapToResponse(TimeOffRequest entity) {
        int days = (int) ChronoUnit.DAYS.between(entity.getStartDate(), entity.getEndDate()) + 1;
        // Check null an toàn
        String approverName = (entity.getApprover() != null && entity.getApprover().getUser() != null) 
                            ? entity.getApprover().getUser().getFullName() 
                            : null;
                            
        return TimeOffResponse.builder()
                .id(entity.getId())
                .employeeName(entity.getEmployee().getUser().getFullName())
                .type(entity.getType().name())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .reason(entity.getReason())
                .status(entity.getStatus().name())
                .daysCount(days)
                .approverName(approverName)
                .build();
    }
}