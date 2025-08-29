-- V3__create_export_jobs_table.sql: Schema for tracking asynchronous export jobs

CREATE TABLE export_jobs (
    id UUID PRIMARY KEY,
    survey_id UUID NOT NULL REFERENCES surveys(id),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, PROCESSING, COMPLETED, FAILED
    s3_url TEXT,
    created_by UUID NOT NULL REFERENCES admin_users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_export_jobs_status ON export_jobs(status);
CREATE INDEX idx_export_jobs_survey_id ON export_jobs(survey_id);
CREATE INDEX idx_export_jobs_created_by ON export_jobs(created_by);
