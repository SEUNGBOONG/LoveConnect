import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 500,           // 동시에 요청할 사용자 수
    duration: '10s',    // 테스트 지속 시간
};

export default function () {
    const url = 'https://api.lovereconnect.co.kr/matches/result';

    const res = http.get(url, {
        headers: {
            Authorization: 'Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3NjI5MzA5NDUsImlhdCI6MTc2MjkyNTU0NSwianRpIjoiNjg1ZWU2YmQtMGE4My00N2ZlLTk3ZjItYzdhMGFiNzAzYTc3IiwibWVtYmVySWQiOjF9.zv-pUvlMt_adUVkKgtfnZcIVeK-FJeFbB9VF7MYTe8A',
        },
    });

    check(res, {
        '✅ status is 200': (r) => r.status === 200,
    });

    sleep(1);
}
