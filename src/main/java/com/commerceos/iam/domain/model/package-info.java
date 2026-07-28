/**
 * Domain models for the IAM module.
 *
 * <p>Contains pure Java domain objects with no framework annotations. Persistence concerns are
 * handled by entity classes in the infrastructure layer.
 *
 * <p>Key classes:
 *
 * <ul>
 *   <li>{@link com.commerceos.iam.domain.model.User} - User aggregate root
 *   <li>{@link com.commerceos.iam.domain.model.Role} - Role value object
 *   <li>{@link com.commerceos.iam.domain.model.Permission} - Permission value object
 *   <li>{@link com.commerceos.iam.domain.model.RefreshToken} - Refresh token value object
 * </ul>
 */
package com.commerceos.iam.domain.model;
