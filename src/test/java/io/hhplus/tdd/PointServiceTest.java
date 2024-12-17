package io.hhplus.tdd;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @InjectMocks
    private PointService pointService; // 테스트 대상

    @Mock
    private UserPointTable userPointTable; // 테스트 대상의 의존성을 해결하기 위해, 예상된 결과를 반환하기 위하여 user table을 mock 으로 설정함

    @Mock
    private UserPoint userPoint; // 테스트 시 사용되는 객체의 팩토리 메소드 기능을 예상하기 위해 mock으로 설정

    /**
     * PATCH  /point/{id}/charge : 포인트를 충전한다.
     * 예외상항 설정
     * - 포인트 충전 시 입력한 id의 사용자가 존재하지 않는 경우
     */
    @Test
    @DisplayName("포인트 충전 시 입력한 id의 사용자가 존재하지 않는 경우")
    public void pointChargeTest1(){
        // given
        final long id = 1L;
        final long amount = 500L;

        // stub
        // 주어진 id로 유저를 검색 했을 때 해당 유저가 존재하지 않음
        when(userPointTable.selectById(id)).thenReturn(null);

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
           pointService.charge(id, amount);
        });

        // then
        assertEquals("입력한 유저가 존재하지 않습니다.", exception.getMessage());
        verify(userPointTable, times(1)).selectById(id);

    }

    /**
     * PATCH  /point/{id}/charge : 포인트를 충전한다.
     * 예외상항 설정
     * - 포인트 충전 시 입력한 amount가 0 또는 입력되지 않았을 경우
     * - 포인트 기본값은 0 으로 설정
     */
    @Test
    @DisplayName("포인트 충전 시 입력한 amount 가 0 또는 입력되지 않았을 경우")
    public void pointChargeTest2(){
        // given
        final long id = 1L;
        final long amount = 0L;

        // stub
        // 주어진 id로 유저를 검색 했을 때 원하는 유저를 반환
        when(userPointTable.selectById(id)).thenReturn(UserPoint.empty(id));

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(id, amount);
        });

        // then
        assertEquals("포인트가 입력되지 않았습니다.", exception.getMessage());
        verify(userPointTable, times(1)).selectById(id);

    }

    /**
     * PATCH  /point/{id}/charge : 포인트를 충전한다.
     * 예외상항 설정
     * - 포인트 충전 시 입력한 amount가 유효한 범위내의 값이 아닌 경우 ( 0 < point <= 1000 )
     */
    @Test
    @DisplayName("포인트 충전 시 입력한 amount가 유효한 범위내의 값이 아닌 경우")
    public void pointChargeTest3(){
        // given
        final long id = 1L;
        final long amount = 1001L;

        // stub
        // 주어진 id로 유저를 검색 했을 때 원하는 유저를 반환
        when(userPointTable.selectById(id)).thenReturn(UserPoint.empty(id));

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(id, amount);
        });

        // then
        assertEquals("포인트는 0 초과 1000 이하 범위 내로 입력해 주세요.", exception.getMessage());
        verify(userPointTable, times(1)).selectById(id);

    }

    /**
     * PATCH  /point/{id}/charge : 포인트를 충전한다.
     *
     */
    @Test
    @DisplayName("포인트 충전 성공 테스트")
    public void pointChargeTest4(){
        // given
        final long id = 1L;
        final long amount = 500L;

        final long addAmount = 100L;

        // 조회 된 유저 정의
        UserPoint selectUser = new UserPoint(id, amount, System.currentTimeMillis());

        // 업데이트 된 유저 정의 ( 조회된 유저의 포인트 충전이 완료 된 )
        UserPoint updatedUser = new UserPoint(selectUser.id(), selectUser.point() + addAmount, System.currentTimeMillis());

        // stub
        // 주어진 id로 조회 된 유저를 반환
        when(userPointTable.selectById(id)).thenReturn(selectUser);

        // id조회로 반환된 유저에게 포인트를 충전해준 userPoint를 반환
        when(userPoint.addPoint(addAmount)).thenReturn(updatedUser);

        // when
        UserPoint returnUser = pointService.charge(id, amount);

        // then
        assertEquals(returnUser.point(), updatedUser.point());
        verify(userPointTable, times(1)).selectById(id);

    }


}
