-- V2__create_survey_tables.sql: Schema for surveys and questions

-- Create surveys table
CREATE TABLE surveys (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT', -- e.g., DRAFT, ACTIVE, CLOSED
    created_by UUID REFERENCES admin_users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create questions table
CREATE TABLE questions (
    id UUID PRIMARY KEY,
    survey_id UUID NOT NULL REFERENCES surveys(id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    question_type VARCHAR(50) NOT NULL, -- TEXT, MULTIPLE_CHOICE_SINGLE, MULTIPLE_CHOICE_MULTI, RATING
    question_order INT NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT false,
    options CLOB, -- Using CLOB for broad compatibility, JSONB is PG-specific
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add indexes for performance
CREATE INDEX idx_surveys_created_by ON surveys(created_by);
CREATE INDEX idx_questions_survey_id ON questions(survey_id);

-- Add a constraint to ensure order is unique per survey
ALTER TABLE questions
ADD CONSTRAINT unique_question_order_per_survey UNIQUE (survey_id, question_order);
