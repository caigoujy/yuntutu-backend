package com.jyniubi.picture.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jyniubi.picture.backend.model.dto.user.UserQueryRequest;
import com.jyniubi.picture.backend.model.entity.User;
import com.jyniubi.picture.backend.model.vo.UserLoginVo;
import com.jyniubi.picture.backend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author zjy
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-04-08 11:39:10
*/
public interface UserService extends IService<User> {
    /**
     *  用户注册
     * @param userAccount 账号
     * @param userPassword 密码
     * @param checkPassword 二次输入密码
     * @return 用户userId
     */
    long userRegister(String userAccount, String userPassword,String checkPassword);

    /**
     * 用户登录
     * @param userAccount 账号
     * @param userPassword 密码
     * @param request 存session
     * @return 脱敏后的用户信息
     */
    UserLoginVo userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录的用户:对用户信息进行脱敏
     * @param user
     * @return
     */
    UserLoginVo getUserLoginVo(User user);

    /**
     * 获取当前登录的用户
     * @param request
     * @return
     */
    User getUserLogin(HttpServletRequest request);

    /**
     *  密码加密
     * @param userPassword
     * @return
     */
    String getEncryptPwd(String userPassword);

    /**
     * 注销
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     *  查询脱敏后的用户信息
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     *  查询脱敏后的用户信息列表
     * @param userList
     * @return
     */
    List<UserVO> getUserVOList(List<User> userList);

    QueryWrapper<User> getUserQueryWrapper(UserQueryRequest userQueryRequest);
}
