package net.xzh.user.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import net.xzh.user.domain.User;
import net.xzh.user.service.UserService;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created 2019/8/29.
 */
@Service
public class UserServiceImpl implements UserService {
	private List<User> userList;

	@Override
	public void create(User user) {
		userList.add(user);
	}

	@Override
	public User getUser(Long id) {
//		try {
//			Thread.sleep(3000L);//模拟业务逻辑
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		List<User> findUserList = userList.stream().filter(userItem -> userItem.getId().equals(id))
				.collect(Collectors.toList());
		if (!CollectionUtils.isEmpty(findUserList)) {
			return findUserList.get(0);
		}
		return null;
	}

	@Override
	public void update(User user) {
		userList.stream().filter(userItem -> userItem.getId().equals(user.getId())).forEach(userItem -> {
			userItem.setUsername(user.getUsername());
			userItem.setPassword(user.getPassword());
		});
	}

	@Override
	public void delete(Long id) {
		User user = getUser(id);
		if (user != null) {
			userList.remove(user);
		}
	}

	@Override
	public User getByUsername(String username) {
		List<User> findUserList = userList.stream().filter(userItem -> userItem.getUsername().equals(username))
				.collect(Collectors.toList());
		if (!CollectionUtils.isEmpty(findUserList)) {
			return findUserList.get(0);
		}
		return null;
	}

	@Override
	public List<User> getUserByIds(List<Long> ids) {
		return userList.stream().filter(userItem -> ids.contains(userItem.getId())).collect(Collectors.toList());
	}

	@PostConstruct
	public void initData() {
		userList = new ArrayList<>();
		userList.add(new User(1L, "xzh", "123456"));
		userList.add(new User(2L, "xuzhihao", "123456"));
		userList.add(new User(3L, "xcg", "123456"));
	}
}
