package com.jyniubi.picture.backend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jyniubi.picture.backend.constant.UserConstant;
import com.jyniubi.picture.backend.exception.BusinessException;
import com.jyniubi.picture.backend.exception.ErrorCode;
import com.jyniubi.picture.backend.exception.ThrowUtils;
import com.jyniubi.picture.backend.mapper.UserMapper;
import com.jyniubi.picture.backend.model.dto.user.UserQueryRequest;
import com.jyniubi.picture.backend.model.entity.User;
import com.jyniubi.picture.backend.model.enums.UserRoleEnum;
import com.jyniubi.picture.backend.model.vo.UserLoginVo;
import com.jyniubi.picture.backend.model.vo.UserVO;
import com.jyniubi.picture.backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author zjy
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-04-08 11:39:10
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 验证参数
        ThrowUtils.throwIf(userAccount == null || userPassword == null || checkPassword == null, ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号过短");
        ThrowUtils.throwIf(userPassword.length() < 8 || checkPassword.length() < 8, ErrorCode.PARAMS_ERROR, "密码长度过短");
        // 验证两次密码是否一致
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次输入密码不一致，请重新输入！");
        // 检查是否重复
        Long account = this.userMapper.selectCount(new QueryWrapper<User>().eq("userAccount", userAccount));
        if (account > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "此账号已存在，请重新输入！");
        }
        // 给密码加密
        String encryptPwd = getEncryptPwd(userPassword);
        // 保存到数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPwd);
        user.setUserName("云图库_" + UUID.randomUUID().toString().substring(0, 8));
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库出错！");
        }
        return user.getId();
    }

    @Override
    public UserLoginVo userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(userAccount == null || userPassword == null, ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号过短");
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "密码长度过短");
        // 密码加密
        String encryptPwd = getEncryptPwd(userPassword);
        // 根据账号和密码查询数据库
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("userAccount", userAccount).eq("userPassword", encryptPwd));
        if (user == null) {
            log.error("user login fail,userAccount not exists or password error");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户不存在或密码错误！");
        }
        // 将脱敏后的用户信息存入session
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return this.getUserLoginVo(user);
    }

    /**
     * 获取脱敏的用户信息
     *
     * @param user 没脱敏的用户信息
     * @return 返回脱敏的用户信息
     */
    @Override
    public UserLoginVo getUserLoginVo(User user) {
        if (user == null) {
            return null;
        }
        UserLoginVo userLoginVo = new UserLoginVo();
        BeanUtils.copyProperties(user, userLoginVo);
        return userLoginVo;
    }

    @Override
    public User getUserLogin(HttpServletRequest request) {
        Object objUser = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) objUser;
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long userId = user.getId();
        user = this.userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return user;
    }

    /**
     * 密码 + 盐 加密
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    @Override
    public String getEncryptPwd(String userPassword) {
        String salt = "picture_123404321";
        return DigestUtils.md5DigestAsHex((salt + userPassword).getBytes());
    }

    /**
     * 注销
     *
     * @param request
     * @return
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        Object objSession = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (objSession == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getUserQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }


}




