package com.company.rms.entity.allocation;

import com.company.rms.entity.hr.Employee;
import com.company.rms.entity.masterdata.JobTitle;
import com.company.rms.entity.project.Project;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity // <-- Quan trọng nhất
@Table(name = "resource_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private Employee requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_title_id", nullable = false)
    private JobTitle jobTitle;

    @Column(name = "quantity")
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RequestStatus status = RequestStatus.OPEN;

    public enum RequestStatus { OPEN, FULFILLED, CANCELLED }
}