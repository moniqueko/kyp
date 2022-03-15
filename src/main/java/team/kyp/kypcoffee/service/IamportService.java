package team.kyp.kypcoffee.service;

import com.nimbusds.jose.shaded.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import team.kyp.kypcoffee.domain.*;
import team.kyp.kypcoffee.mapper.PayMapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class IamportService {

    private final PayMapper mapper;
    private final OrderInfoServiceImpl orderInfoServiceImpl;

    @Autowired
    public IamportService(PayMapper mapper, OrderInfoServiceImpl orderInfoServiceImpl) {
        this.orderInfoServiceImpl = orderInfoServiceImpl;
        this.mapper = mapper;
    }

    private final String imp_key = "3208902506195454";
    private final String imp_secret = "5f2aeafc2377d15f2bafad578b698cc21f3255a6188f3b7e3dce66a5efd8151002e88e4115c515eb";
    private RestTemplate restTemplate = new RestTemplate();
    private HttpHeaders headers = new HttpHeaders();
    private JSONObject body = new JSONObject();

    private IamportDo getToken() {
        headers.setContentType(MediaType.APPLICATION_JSON);
        body.put("imp_key", imp_key);
        body.put("imp_secret", imp_secret);
        try {
            HttpEntity<JSONObject> entity = new HttpEntity<>(body,headers);

            IamportDo token = restTemplate.postForObject("https://api.iamport.kr/users/getToken", entity, IamportDo.class);
            System.out.println(token+" FULLtoken");

            return token;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("gettoken에서 오류가 발생");
        }finally{
            headerAndBodyClear();
        }
        return null;
    }
    public boolean confrimBuyerInfo(String imp_uid,int totalPrice) {
        IamportDo iamprotDto = getToken();
        try {
            if(iamprotDto==null){
                throw new Exception();
            }
            headers.add("Authorization",(String) iamprotDto.getResponse().get("access_token"));
            HttpEntity<JSONObject>entity = new HttpEntity<JSONObject>(headers);

            BuyerInfoDo buyerInfo = restTemplate.postForObject("https://api.iamport.kr/payments/"+imp_uid+"",entity,BuyerInfoDo.class);
            System.out.println(buyerInfo+" fullinfor");

            if(totalPrice == (int)buyerInfo.getResponse().get("amount")){

                return true;
            }
        } catch (Exception e) {

            e.printStackTrace();
            System.out.println("getBuyerInfo 검증 실패");

        }finally{
            headerAndBodyClear();
        }
        return false;
    }
    public void cancleBuy(String imp_uid,int returnPrice) {
        try {
            IamportDo iamprotDo = getToken();
            if(iamprotDo == null){
                throw new Exception();
            }

            headers.add("Authorization",(String) iamprotDo.getResponse().get("access_token"));
            body.put("imp_uid", imp_uid);

            if(returnPrice!=0){
                body.put("amount", returnPrice);
            }

            HttpEntity<JSONObject>entity = new HttpEntity<JSONObject>(body, headers);
            PayCancleDo cancle = restTemplate.postForObject("https://api.iamport.kr/payments/cancel",entity,PayCancleDo.class);

            System.out.println(cancle+" full cancle");

        } catch (Exception e) {

            e.printStackTrace();
            System.out.println("cancleBuy가 실패 했습니다 직접 환불 바랍니다");
            throw new RuntimeException("환불에 실패 했습니다 다시시도 바랍니다");

        }finally{
            headerAndBodyClear();
        }
    }
    private void headerAndBodyClear(){
        headers.clear();
        body.clear();
    }

    public JSONObject makeJson(boolean result,String sucupdatepwd) {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("result",result);
        jsonObject.put("messege", sucupdatepwd);
        return jsonObject;
    }

    public void insertPay(String imp_uid, List<String> cartNum, int totalPrice, String dateString, int memberNum) throws Exception {

        ArrayList<Integer> cartNums = new ArrayList<>();

        for (String s : cartNum) {

            cartNums.add(Integer.valueOf(s));
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date = formatter.parse(dateString);

        List<Product_info> pInfos = orderInfoServiceImpl.productInfo(cartNums);

        if (cartNum.size() >= 2) {

        } else if (cartNum.size() == 1) {

            Payment payment = new Payment(pInfos.get(0).getProductCode(), pInfos.get(0).getCartQuantity(), totalPrice, imp_uid, date, memberNum);
            mapper.insertPay(payment);
        }
    }

    public List<Payment> selectPayment(int memberNum) {

        return mapper.selectPaymentByMemberNum(memberNum);
    }
}