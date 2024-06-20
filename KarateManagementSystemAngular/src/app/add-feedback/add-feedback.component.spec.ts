import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddFeedbackComponent } from './add-feedback.component';

describe('AddFeedbackComponent', () => {
  let component: AddFeedbackComponent;
  let fixture: ComponentFixture<AddFeedbackComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddFeedbackComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AddFeedbackComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
