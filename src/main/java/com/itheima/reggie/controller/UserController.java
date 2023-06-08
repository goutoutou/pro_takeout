package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 发送手机验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody  User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();
        if(StringUtils.isNotEmpty(phone)){
            //生成随机四位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("密码 code={}",code);

            //调用阿里云短信服务API完成发送短信
           // SMSUtils.sendMessage("reji","",phone,code);
            //1将生成的验证码保存到Session
            //2session.setAttribute(phone,code);//数据存储到session中
            //将生成的验证码缓存到redis中，设置有效期为五分钟
            redisTemplate.opsForValue().set(phone,code,50, TimeUnit.MINUTES);
            return R.success("验证码发送成功");
        }
        return R.error("号码为空");
    }
    /**
     * 登录按钮
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
        //方法1从session中获取保存的验证码
        //Object codeInSession = session.getAttribute(phone);
        //方法2 从Redis中取出验证码
        Object codeInSession = redisTemplate.opsForValue().get(phone);
        //进行验证码的比对
        if (codeInSession!=null && codeInSession.equals(code)){
            //比对成功，判断是否是新用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user=userService.getOne(queryWrapper);
            if (user==null){
                 user = new User();
                 user.setPhone(phone);
                 user.setStatus(1);
                 userService.save(user);
            }
           // session.setAttribute("user",user.getId());
            //如果用户登录成功，删除Redis中缓存的验证码
            redisTemplate.delete(phone);
            return R.success(user);
        }
        return R.error("登录失败");
    }


}
