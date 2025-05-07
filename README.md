Health connect example 

kotlin jetpack compose

안드로이드 health connect 을 이용해 보기 중요한 것은 manigest의 설정을 해야 한다는 것입니다. 

            <!--
            앱은 이 인텐트를 처리하고 사용자 데이터의 사용 및 처리 방식을 설명하는 적절한 개인정보 보호정책을 표시해야 합니다.
            이 인텐트는 사용자가 Health Connect 권한 대화상자에서 "개인정보 보호정책" 링크를 클릭하면 앱으로 전송됩니다.
            -->
            <intent-filter>
                <action android:name="androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE" />
            </intent-filter>

            <!-- Permission handling for Android 14 -->
            <intent-filter>
                <action android:name="android.intent.action.VIEW_PERMISSION_USAGE"/>
                <category android:name="android.intent.category.HEALTH_PERMISSIONS"/>
            </intent-filter>

또한 Health API 을 사용해 만보계을 만들어 볼 수 있을 것 같습니다.  
