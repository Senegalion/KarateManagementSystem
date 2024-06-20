import {Component, OnInit} from '@angular/core';
import {FeedbackService} from "./feedback.service";
import {ActivatedRoute} from "@angular/router";
import {NgForOf} from "@angular/common";

@Component({
  selector: 'app-feedback',
  standalone: true,
  imports: [
    NgForOf
  ],
  templateUrl: './feedback.component.html',
  styleUrl: './feedback.component.css'
})
export class FeedbackComponent implements OnInit {
  feedbackList: any[] = [];
  sessionId: number;

  constructor(private feedbackService: FeedbackService, private route: ActivatedRoute) {
    this.sessionId = this.route.snapshot.params['id'];
  }

  ngOnInit(): void {
    this.loadFeedback();
  }

  loadFeedback() {
    this.feedbackService.getFeedbackForTraining(this.sessionId).subscribe(data => {
      this.feedbackList = data;
    });
  }
}
