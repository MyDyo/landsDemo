package com.demo.listener;//package com.nwu.listener;
//
//import com.alibaba.excel.context.AnalysisContext;
//import com.alibaba.excel.event.AnalysisEventListener;
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.nwu.entities.excel.ProjectCrossExcel;
//import com.nwu.entities.sciManagement.ProjectCross;
//import com.nwu.exceptionhandler.GlobalExceptionHandler;
//import com.nwu.exceptionhandler.SciManagementException;
//import com.nwu.service.ScientificResearchManagementService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//
//public class ProjectCrossExcelListener extends AnalysisEventListener<ProjectCrossExcel> {
//
//    public ScientificResearchManagementService scientificResearchManagementService;
//
//    public ProjectCrossExcelListener(ScientificResearchManagementService scientificResearchManagementService) {
//        this.scientificResearchManagementService = scientificResearchManagementService;
//    }
//    //读取excel内容，一行一行进行的读取
//    @Override
//    public void invoke(ProjectCrossExcel projectCrossExcel, AnalysisContext analysisContext) {
//        if (projectCrossExcel == null) {
//            throw new SciManagementException(20001, "excel文件数据为空");
//        }
//        ProjectCross projectCross = new ProjectCross();
////        System.out.println(projectCrossExcel.getProjectName());
//        projectCross.setProjectName(projectCrossExcel.getProjectName());
////        if(projectCross.getProjectId()==null){
////            return;
////        }
//        scientificResearchManagementService.save(projectCross);
//
////        Integer pid =projectCross.getProjectId();
////        ProjectCross projectCrossTwo = this.existTwoProjectCross(scientificResearchManagementService, projectCrossExcel.getProjectNumber(),pid);
////        if(projectCrossTwo == null){
////            projectCrossTwo =new ProjectCross();
////            projectCrossTwo.setProjectNumber(pid);
////            projectCrossTwo.setProjectName(projectCrossExcel.getProjectNumber());  //一级分类名称
////            scientificResearchManagementService.save(projectCrossTwo);
////
////        }
////    }
//    }
//
////    private ProjectCross existOneProjectCross(ScientificResearchManagementService scientificResearchManagementService,String name){
////        QueryWrapper<ProjectCross> wrapper =new QueryWrapper<>();
////        wrapper.eq("project_name",name);
////        wrapper.eq("project_number","12");
////        ProjectCross one = scientificResearchManagementService.getOne(wrapper);
////        return one;
////    }
////    private ProjectCross existTwoProjectCross(ScientificResearchManagementService scientificResearchManagementService, Integer name, Integer pid){
////
////        QueryWrapper<ProjectCross> wrapper =new QueryWrapper<>();
////        wrapper.eq("project_name",name);
////        wrapper.eq("project_number","pid");
////        ProjectCross two = scientificResearchManagementService.getOne(wrapper);
////        return two;
////    }
//
//    @Override
//    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
//
//    }
//}
