package cn.yjl.game.dto;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(chain = true)
public class ResponseJsonDto {

    private boolean success = true;

    private String errMsg;

    private int errCode;

    private int status = 200;

    private Object data;

    private boolean applicationError = false;

    @Getter
    private Map<String, Object> claims = new HashMap<String, Object>();

    public ResponseJsonDto addClaim(String key, Object value) {
        this.claims.put(key, value);
        return this;
    }

    public <T> ResponseJsonDto addClaims(Map<String, T> claims) {
        this.claims.putAll(claims);
        return this;
    }

    public ResponseJsonDto fail(String errMsg, int errCode) {
        this.success = false;
        this.errMsg = errMsg;
        this.errCode = errCode;
        return this;
    }

    public ResponseJsonDto fail(String errMsg) {
        this.success = false;
        this.errMsg = errMsg;
        return this;
    }
}
