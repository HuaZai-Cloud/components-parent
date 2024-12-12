package cloud.huazai.datatransmission;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * ResultResponse
 *
 * @author devon
 * @since 2024/12/12
 */
@Getter
@Setter
@ToString
public class ResponseResult extends Response{

    private boolean success;

    private String code;

    private String errMessage;


}
