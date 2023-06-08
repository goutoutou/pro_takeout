package com.itheima.reggie.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;
    /**
     * 用户登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    //注解@RequestBody的原因是要将前端浏览器的请求包含账号密码封装成Employee对象
    //登录方法
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //将页面提交的密码进行加密处理
        String password=employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());//java自己的工具类，提供数据加密的
        //根据用户名查询用户
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        //::相当于创建了一个Employee对象并且调用了getname方法
        //LambdaQueryWrapper是mybatis plus中的一个条件构造器对象，只是是需要使用Lambda 语法使用 Wrapper
        Employee emp = employeeService.getOne(queryWrapper);//getone是根据wrapper获取一条记录
        //查看数据中是否存在这个用户
        if(emp==null){
            return R.error("登录失败");
        }
        //4进行密码比对
        if(!emp.getPassword().equals(password)){
            return R.error("登录失败");
        }
        //5查看员工状态是否禁用0表示禁用
        if(emp.getStatus()==0) return R.error("账号已禁用");
        //6登录成功
        request.getSession().setAttribute("employee",emp.getId());//存入session
        return R.success(emp);
    }

    /**
     * 退出登录
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public  R<String> logout(HttpServletRequest request){
        //清理session中员工id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param request
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工，员工信息：{}",employee.toString());
        //密码加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
       //   employee.setCreateTime(LocalDateTime.now());
      //  employee.setUpdateTime(LocalDateTime.now());
      //  Long empId =(Long) request.getSession().getAttribute("employee");
      //  employee.setCreateUser(empId);
      //  employee.setUpdateUser(empId);
        employeeService.save(employee);//插入一条记录
        return R.success("新增员工成功");
    }
    /**
     * 页面显示
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize, String name){
        log.info("page={},pageSize={},name={}",page,pageSize,name);
        //string name是查询传进来的参数
        log.info("page={},pageSize={},name={}",page,pageSize,name);
        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //添加排序条件,更新时间
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 更改员工状态信息是否禁用
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());
        long id = Thread.currentThread().getId();
        log.info("线程id为：{}",id);
       // Long empId = (Long) request.getSession().getAttribute("employee");
        //employee.setUpdateTime(LocalDateTime.now());
       // employee.setUpdateUser(empId);
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }
    /**
     * 员工信息修改成功
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("员工查询根据id");
        Employee employee = employeeService.getById(id);
        if (employee!=null)
          return R.success(employee);
        return R.error("没有查询到对应员工信息");
    }
}
