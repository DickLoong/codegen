package ${config_package_name};

import lombok.Data;

import com.lexing360.dmp.annotation.DBConfig;
import lombok.Data;
import javax.persistence.*;

@Data
@DBConfig
@Entity
@Table
public class ${myClass.className} {
    <#assign f_index = 0 />
    <#list myClass.fieldList as field>
        //${field.fieldRemarks}
    <#if f_index == 0>
        @Id
    <#else>
        @Column
    </#if>
        private ${field.fieldType} ${field.fieldName};
    <#assign f_index = f_index+1 />
    </#list>
}