import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserSignedUpSessionsComponent } from './user-signed-up-sessions.component';

describe('UserSignedUpSessionsComponent', () => {
  let component: UserSignedUpSessionsComponent;
  let fixture: ComponentFixture<UserSignedUpSessionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserSignedUpSessionsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(UserSignedUpSessionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
