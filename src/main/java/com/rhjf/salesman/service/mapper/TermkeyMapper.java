package com.rhjf.salesman.service.mapper;

import org.springframework.stereotype.Component;

import com.rhjf.account.modle.domain.salesman.TermKey;

@Component
public interface TermkeyMapper {

	public int sign(TermKey terMkey);

	public TermKey queryUserKey(String UserID);

	public int addTermKey(TermKey termKey);

}
