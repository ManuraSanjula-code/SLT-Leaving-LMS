package com.slt.peotv.lmsmangmentservice.entity;

import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "componet_admins")
public class ComponetAdminsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String componetID;
    @ManyToOne
    private EmployeeEntity employee;
    private Date approvedDate;
    private Integer highestRolePriority;
    private Boolean isAccepted;
    private String profilePic;

    public ComponetAdminsEntity() {
    }

    public ComponetAdminsEntity(Integer id, String componetID, EmployeeEntity employee, Date approvedDate, Integer highestRolePriority, Boolean isAccepted, String profilePic) {
        this.id = id;
        this.componetID = componetID;
        this.employee = employee;
        this.approvedDate = approvedDate;
        this.highestRolePriority = highestRolePriority;
        this.isAccepted = isAccepted;
        this.profilePic = profilePic;
    }

    public ComponetAdminsEntity(String componetID, EmployeeEntity employee, Integer highestRolePriority) {
        this.componetID = componetID;
        this.employee = employee;
        this.highestRolePriority = highestRolePriority;
    }

    public ComponetAdminsEntity(String componetID, EmployeeEntity employee, Integer highestRolePriority, Boolean isAccepted) {
        this.componetID = componetID;
        this.employee = employee;
        this.highestRolePriority = highestRolePriority;
        this.isAccepted = isAccepted;
    }

    public static ComponetAdminsEntity create(String componetID, EmployeeEntity employee, Integer highestRolePriority) {
        return new ComponetAdminsEntity(componetID, employee, highestRolePriority);
    }

    public static ComponetAdminsEntity createAccepted(String componetID, EmployeeEntity employee, Integer highestRolePriority) {
        ComponetAdminsEntity entity = new ComponetAdminsEntity(componetID, employee, highestRolePriority);
        entity.setIsAccepted(true);
        entity.setApprovedDate(new Date());
        return entity;
    }

    public static ComponetAdminsEntity createPending(String componetID, EmployeeEntity employee, Integer highestRolePriority) {
        ComponetAdminsEntity entity = new ComponetAdminsEntity(componetID, employee, highestRolePriority);
        entity.setIsAccepted(false);
        return entity;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getComponetID() {
        return componetID;
    }

    public void setComponetID(String componetID) {
        this.componetID = componetID;
    }

    public EmployeeEntity getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeEntity employee) {
        this.employee = employee;
    }

    public Date getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(Date approvedDate) {
        this.approvedDate = approvedDate;
    }

    public Integer getHighestRolePriority() {
        return highestRolePriority;
    }

    public void setHighestRolePriority(Integer highestRolePriority) {
        this.highestRolePriority = highestRolePriority;
    }

    public Boolean getIsAccepted() {
        return isAccepted;
    }

    public void setIsAccepted(Boolean isAccepted) {
        this.isAccepted = isAccepted;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponetAdminsEntity that = (ComponetAdminsEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(componetID, that.componetID) &&
                Objects.equals(employee, that.employee) &&
                Objects.equals(approvedDate, that.approvedDate) &&
                Objects.equals(highestRolePriority, that.highestRolePriority) &&
                Objects.equals(isAccepted, that.isAccepted) &&
                Objects.equals(profilePic, that.profilePic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, componetID, employee, approvedDate, highestRolePriority, isAccepted, profilePic);
    }

    @Override
    public String toString() {
        return "ComponetAdminsEntity{" +
                "id=" + id +
                ", componetID='" + componetID + '\'' +
                ", employee=" + employee +
                ", approvedDate=" + approvedDate +
                ", highestRolePriority=" + highestRolePriority +
                ", isAccepted=" + isAccepted +
                ", profilePic='" + profilePic + '\'' +
                '}';
    }
}